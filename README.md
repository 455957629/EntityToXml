# EntityToXml

实体类输出成xml格式


目前能处理多重List

    <voucher>
        <voucher_head>
            <id>100</id>
            <strs>
                <testlist>
                    <dns>1</dns>
                    <result>dns 1</result>
                    <items>
                        <testitem>
                            <item>item1</item>
                        </testitem>
                        <testitem>
                            <item>item2</item>
                        </testitem>
                    </items>
                </testlist>
                <testlist>
                    <dns>2</dns>
                    <result>dns 2</result>
                    <items>
                        <testitem>
                            <item>item1</item>
                        </testitem>
                        <testitem>
                            <item>item2</item>
                        </testitem>
                    </items>
                </testlist>
            </strs>
        </voucher_head>
    </voucher>
